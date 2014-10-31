package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08.03.2011
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableFileSystemLog extends Indexable {

    @Column
    @NotNull
    @Size(min = 2, max = 256 * 8)
    @Attribute(field = false, description = "This is the path to the log folder")
    private String path;
    @Column
    @NotNull
    @Size(min = 2, max = 64)
    @Attribute(field = false, description = "The suffix that we expect the log files to have")
    private String suffix = ".log";
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "This is the file name field in the Lucene index")
    private String fileFieldName;
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "This is the path name field in the Lucene index")
    private String pathFieldName;
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "This is the line number field in the Lucene index")
    private String lineFieldName;
    @Column
    @NotNull
    @Size(min = 2, max = 256)
    @Attribute(field = false, description = "This is the content field in the Lucene index")
    private String contentFieldName;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public String getFileFieldName() {
        return fileFieldName;
    }

    public void setFileFieldName(final String fileFieldName) {
        this.fileFieldName = fileFieldName;
    }

    public String getPathFieldName() {
        return pathFieldName;
    }

    public void setPathFieldName(final String pathFieldName) {
        this.pathFieldName = pathFieldName;
    }

    public String getLineFieldName() {
        return lineFieldName;
    }

    public void setLineFieldName(final String lineFieldName) {
        this.lineFieldName = lineFieldName;
    }

    public String getContentFieldName() {
        return contentFieldName;
    }

    public void setContentFieldName(final String contentFieldName) {
        this.contentFieldName = contentFieldName;
    }
}
