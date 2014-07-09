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

    @Column(length = 4096)
    private String dump;
    @Column(length = 4096)
    private String predicate;
    @Column
    private Boolean result;

    @ElementCollection
    private List<String> rules;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDump() {
        return dump;
    }

    public void setDump(String dump) {
        this.dump = dump;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }
}
