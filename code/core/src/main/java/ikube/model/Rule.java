package ikube.model;

import javax.persistence.*;
import java.util.List;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Rule extends Persistable {

    @Column
    private String action;
    @Column
    private String server;
    @Column
    private String indexContext;

    @Lob
    @Column(length = 4096)
    private String predicate;
    @Column
    private Boolean result;

    @ElementCollection
    private List<Boolean> evaluations;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getServer() {
        return server;
    }

    public void setServer(final String server) {
        this.server = server;
    }

    public String getIndexContext() {
        return indexContext;
    }

    public void setIndexContext(final String indexContext) {
        this.indexContext = indexContext;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(final String predicate) {
        this.predicate = predicate;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(final Boolean result) {
        this.result = result;
    }

    public List<Boolean> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(final List<Boolean> evaluations) {
        this.evaluations = evaluations;
    }
}
