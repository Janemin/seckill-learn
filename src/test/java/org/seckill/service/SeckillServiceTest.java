package org.seckill.service;


import java.util.List;

import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
	"classpath:spring/spring-dao.xml",
	"classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
	
	private final Logger logger = LoggerFactory.getLogger(Theories.class);
	
	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillLict() {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}",list);
/*		for (Seckill seckill : list) {
			System.out.println(seckill.toString());
		}*/
	}

	@Test
	public void testGetById() {
		Seckill seckill = seckillService.getById(1000l);
		logger.info("seckill={}",seckill);
	}

	@Test
	public void testExportSeckillUrl() {
		long id = 1000;
		Exposer  exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}",exposer);
		
	}

	@Test
	public void testExecuteSeckill() {
		long id = 1000;
		long phone = 15651385577l;
		String md5 = "737545a3502bc5b3d09b9b05d2becbb1";
		SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
		logger.info("seckillExecution={}",seckillExecution);
		
	}
	
	@Test
	public void testExecuteSeckillByProcedure() {
		long id = 1005;
		long phone = 15651385588l;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if(exposer.isExposed()) {
			logger.info("exposer"+exposer);
			String md5 = exposer.getMd5();
			try {
				SeckillExecution seckillExecution = 
						seckillService.executeSeckillByProcedure(id, phone, md5);
				logger.info("seckillExecution"+seckillExecution);
			} catch (Exception e) {
				logger.info(e.getMessage(), e);
			}
		}else {
			logger.warn("exposer"+exposer);
		}
		
	}
	


}
