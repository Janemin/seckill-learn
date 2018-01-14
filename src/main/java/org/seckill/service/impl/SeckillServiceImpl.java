package org.seckill.service.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;


//@Component @Service @Dao @ controller
@Service
public class SeckillServiceImpl implements SeckillService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//注入service依赖
	@Autowired  //@Resource  , @Inject
	private SeckillDao seckillDao;
	
	@Autowired
	private SuccessKilledDao successKilledDao;
	
	@Autowired
	private RedisDao redisDao;
	
	//md5盐值字符串，用于混淆md5
	private final String slat = "dfshgsdafgsdafsatrnghktg34q6thdga";

	public List<Seckill> getSeckillList() {		
		return seckillDao.queryAll(0, 100);
	}

	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {	
		//优化点：缓存优化:超时基础上的维护一致性
		//1.访问redis
		Seckill seckill = redisDao.getSeckill(seckillId);
		if(null == seckill) {
			//访问数据库
			seckill = seckillDao.queryById(seckillId);
			if(null == seckill) {
				return new Exposer(false, seckillId);			
			}else{
				redisDao.putSeckill(seckill);
			}
		}		
		
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		//当前系统时间
		Date nowTime = new Date();
		
		if (nowTime.getTime() < startTime.getTime() || 
				nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), 
					startTime.getTime(), endTime.getTime());			
		}
		//转化特定字符串的过程，不可逆
		String md5 = getMD5(seckillId); 					
		return new Exposer(true, md5, seckillId);
	}
	
	private String getMD5(long seckillId) {
		String base = seckillId+"/"+ slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}
	
	/* 
	 * 使用注解控制事务方法的有点
	 * 1.开发团队达成一致的约定，明确标注事务方法的编程风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，prc/http请求 或者 剥离到事务方法外部。
	 * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制。
	 */
	@Transactional
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if(md5 == null || !md5.equals(getMD5(seckillId))) {
			return new SeckillExecution(seckillId, 
					SeckillStateEnum.DATA_REWRITE);
		}
		
		//执行秒杀逻辑：减库存 + 记录购买行为
		Date nowTime = new Date();
		try {
			//记录购买行为
			int insertCount = successKilledDao.insertSucessKilled(seckillId, userPhone);
			//唯一：seckillId + userPhone
			if (insertCount <= 0){
				//重复秒杀
				throw new RepeatKillException("Seckill repeated");
			} else {
				//减库存
				int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
				if (updateCount <= 0){
					//没有更新记录秒杀结束
					throw new SeckillCloseException("Seckill is closed");			
				} else {
					SuccessKilled successKilled = successKilledDao.
							queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, 
							SeckillStateEnum.SUCCESS, successKilled);					
				}				
			}
		} catch (SeckillCloseException e1) {
			throw e1;
			
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			//所有编译期异常，转化为运行期异常
			throw new SeckillException("Seckill inner error" + e.getMessage());
		}
	}

	public SeckillExecution executeSeckillByProcedure(long seckillId, 
			long userPhone, String md5) {
		if(md5 == null || !md5.equals(getMD5(seckillId))) {
			return new SeckillExecution(seckillId, 
					SeckillStateEnum.DATA_REWRITE);
		}
		
		Date killTime = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("seckill_id", seckillId);
		map.put("phone", userPhone);
		map.put("killTime", killTime);
		map.put("result", null);
		
		try {
			seckillDao.killByProcedure(map);
			int result = MapUtils.getInteger(map, "result", -2);//没有为-2
			if (result == 1) {
				SuccessKilled successKilled = successKilledDao.
						queryByIdWithSeckill(seckillId, userPhone);
				return new SeckillExecution(seckillId, 
						SeckillStateEnum.SUCCESS, successKilled);
			} else {
				return new SeckillExecution(seckillId, 
						SeckillStateEnum.stateOf(result));
			}
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			return new SeckillExecution(seckillId, 
					SeckillStateEnum.INNER_ERROR);
		}
		
	}
}
