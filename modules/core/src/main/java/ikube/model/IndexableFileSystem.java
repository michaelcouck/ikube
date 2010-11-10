package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import javax.persistence.Entity;

@Entity()
public class IndexableFileSystem extends Indexable<IndexableFileSystem> {

	private String path;
	private String nameFieldName;
	private String pathFieldName;
	private String lastModifiedFieldName;
	private String lengthFieldName;
	private String contentFieldName;
	private String excludedPattern;
	private String includedPattern;

	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.TRUE;

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(V visitor) {
		visitor.visit(this);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getNameFieldName() {
		return nameFieldName;
	}

	public void setNameFieldName(String nameFieldName) {
		this.nameFieldName = nameFieldName;
	}

	public String getPathFieldName() {
		return pathFieldName;
	}

	public void setPathFieldName(String pathFieldName) {
		this.pathFieldName = pathFieldName;
	}

	public String getLastModifiedFieldName() {
		return lastModifiedFieldName;
	}

	public void setLastModifiedFieldName(String lastModifiedFieldName) {
		this.lastModifiedFieldName = lastModifiedFieldName;
	}

	public String getLengthFieldName() {
		return lengthFieldName;
	}

	public void setLengthFieldName(String lengthFieldName) {
		this.lengthFieldName = lengthFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getExcludedPattern() {
		return excludedPattern;
	}

	public void setExcludedPattern(String excludedPattern) {
		this.excludedPattern = excludedPattern;
	}

	public String getIncludedPattern() {
		return includedPattern;
	}

	public void setIncludedPattern(String includedPattern) {
		this.includedPattern = includedPattern;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(final boolean stored) {
		this.stored = stored;
	}

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(final boolean analyzed) {
		this.analyzed = analyzed;
	}

	public boolean isVectored() {
		return vectored;
	}

	public void setVectored(final boolean vectored) {
		this.vectored = vectored;
	}

}
