package org.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
    
	@Resource
	private SuccessKilledDao successKillDao;
	
	@Test
	public void testInsertSucessKilled() {
		long id = 1000L;
		long phone = 15651385578L;
		int insertCount = successKillDao.insertSucessKilled(id, phone);
		System.out.println(insertCount);
		
	}

	@Test
	public void testQueryByIdWithSeckill() {
		long id = 1000L;
		long phone = 15651385578L;
		SuccessKilled successKilled = successKillDao.queryByIdWithSeckill(id, phone);
		System.out.println(successKilled);		
	}

}
