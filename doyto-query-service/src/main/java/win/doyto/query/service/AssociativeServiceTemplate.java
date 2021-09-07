package win.doyto.query.service;

/**
 * AssociativeServiceTemplate
 *
 * @author f0rb on 2021-06-06
 * @deprecated using {@link TemplateAssociativeService} instead. This one will be deleted in next minor version.
 */
@SuppressWarnings("java:S1133")
@Deprecated
public class AssociativeServiceTemplate<L, R> extends TemplateAssociativeService<L, R> {
    public AssociativeServiceTemplate(String table, String left, String right) {
        super(table, left, right);
    }

    public AssociativeServiceTemplate(String table, String left, String right, String createUserColumn) {
        super(table, left, right, createUserColumn);
    }
}
