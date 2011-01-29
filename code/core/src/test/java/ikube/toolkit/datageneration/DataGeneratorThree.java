package ikube.toolkit.datageneration;

import ikube.model.Attachment;
import ikube.model.Faq;

import java.sql.Timestamp;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DataGeneratorThree extends ADataGenerator {

	private String persistenceUnitName = "IkubePersistenceUnit";
	private EntityManager entityManager;

	private int iterations = 0;

	public DataGeneratorThree(int iterations) {
		this.iterations = iterations;
	}

	public void before() throws Exception {
		super.before();
		entityManager = Persistence.createEntityManagerFactory(persistenceUnitName).createEntityManager();
	}

	@Override
	public void generate() throws Exception {
		EntityTransaction transaction = entityManager.getTransaction();
		for (int i = 0; i < iterations; i++) {
			if (!transaction.isActive()) {
				transaction.begin();
			}
			Faq faq = createFaq();
			entityManager.persist(faq);
			if (i % 10 == 0) {
				transaction.commit();
				transaction = entityManager.getTransaction();
			}
		}
	}

	protected Faq createFaq() {
		Faq faq = new Faq();
		faq.setAnswer(generateText(200, 1024));
		faq.setCreationTimestamp(new Timestamp(System.currentTimeMillis()));
		faq.setCreator(generateText(3, 64));
		faq.setModifiedTimestamp(new Timestamp(System.currentTimeMillis()));
		faq.setModifier(generateText(2, 64));
		faq.setPublished(Math.random() > 0.5 ? Boolean.TRUE : Boolean.FALSE);
		faq.setQuestion(generateText(200, 1024));
		faq.setAttachments(createAttachments(faq));
		return faq;
	}

	protected Set<Attachment> createAttachments(Faq faq) {
		Set<Attachment> attachments = new TreeSet<Attachment>();
		for (String fileName : fileContents.keySet()) {
			Attachment attachment = new Attachment();
			attachment.setAttachment(fileContents.get(fileName));
			attachment.setFaq(faq);
			attachment.setLength(attachment.getAttachment().length);
			attachment.setName(fileName);
			attachments.add(attachment);
		}
		return attachments;
	}

	public void after() throws Exception {
		super.after();
		entityManager.close();
	}

}
