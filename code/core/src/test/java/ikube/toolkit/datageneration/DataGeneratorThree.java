package ikube.toolkit.datageneration;

import ikube.model.Attachment;
import ikube.model.Faq;
import ikube.toolkit.ThreadUtilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DataGeneratorThree extends ADataGenerator {

	private String persistenceUnitName = "IkubePersistenceUnit";

	private int iterations = 0;
	private int threads;

	public DataGeneratorThree(int threads, int iterations) {
		this.threads = threads;
		this.iterations = iterations;
	}

	public void before() throws Exception {
		super.before();
	}

	@Override
	public void generate() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		for (int i = 0; i < this.threads; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					EntityManager entityManager = entityManagerFactory.createEntityManager();
					for (int i = 0; i < iterations; i++) {
						if (!entityManager.getTransaction().isActive()) {
							entityManager.getTransaction().begin();
						}
						Faq faq = createFaq();
						entityManager.persist(faq);
						if (i % 10 == 0) {
							logger.info("Comitting : " + i + ", " + this.hashCode());
							entityManager.getTransaction().commit();
						}
					}
				}
			});
			threads.add(thread);
			thread.start();
			Thread.sleep(3000);
		}
		ThreadUtilities.waitForThreads(threads);
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
			// logger.info("Setting attachment : " + fileName);
			Attachment attachment = new Attachment();
			attachment.setAttachment(fileContents.get(fileName));
			attachment.setFaq(faq);
			attachment.setLength(attachment.getAttachment().length);
			attachment.setName(fileName);
			boolean added = attachments.add(attachment);
			if (!added) {
				logger.info("Didn't add attachment : " + attachment);
			}
		}
		return attachments;
	}

	public void after() throws Exception {
		super.after();
	}

}
