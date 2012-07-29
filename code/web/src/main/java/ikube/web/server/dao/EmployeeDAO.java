package ikube.web.server.dao;

import ikube.web.shared.dto.EmployeeDTO;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;



@Repository("employeeDAO")
public class EmployeeDAO extends JpaDAO<Long, EmployeeDTO> {
	
	@Autowired
	EntityManagerFactory entityManagerFactory;
	
	@PostConstruct
	public void init() {
		super.setEntityManagerFactory(entityManagerFactory);
	}
	
}