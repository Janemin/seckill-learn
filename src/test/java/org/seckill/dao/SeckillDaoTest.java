package org.seckill.dao;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * 配置spring和junit整合，junit启动时加载springIoc容器
 * spring-test,junit
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
	
	//注入Dao实现类依赖
	@Resource
	private SeckillDao seckillDao;
	
	@Test
	public void testReduceNumber() throws Exception {
		
		Seckill seckillBefore = seckillDao.queryById(1000L);
		System.out.println(seckillBefore.toString());
		
		Date killTime = new Date();
		int updateCount = seckillDao.reduceNumber(1000L, killTime);
		System.out.println(updateCount);
		
		Seckill seckillAfter = seckillDao.queryById(1000L);
		System.out.println(seckillAfter.toString());
				
	}
	
	@Test
	public void testQueryById() throws Exception {
		
		long id = 1000;
		Seckill seckill = seckillDao.queryById(id);
		System.out.println(seckill.toString());
		
	}
	
	@Test
	public void testQueryAll() throws Exception {
		
		List<Seckill> seckillList = seckillDao.queryAll(0, 100);
		for(Seckill seckill : seckillList) {
			System.out.println(seckill.toString());			
		}

				
	}

}
