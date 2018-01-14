package org.seckill.dao.cache;

import static org.junit.Assert.*;

import java.security.PrivilegedActionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
	
	private long id = 1001l;
	
	@Autowired
	private RedisDao redisdao;
    
	@Autowired
	private SeckillDao seckillDao;
	@Test
	public void testSeckill(){
		Seckill seckill = redisdao.getSeckill(id);
		if(null == seckill) {
			seckill = seckillDao.queryById(id);
			if(null != seckill) {
				String result = redisdao.putSeckill(seckill);
				System.out.println(result);
				seckill = redisdao.getSeckill(id);
				System.out.println(seckill);
			}			
		}		
	}



}
