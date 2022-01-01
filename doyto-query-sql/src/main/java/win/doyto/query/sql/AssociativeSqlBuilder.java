package win.doyto.query.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AssociativeSqlBuilder
 *
 * @author f0rb on 2019-06-13
 * @deprecated use {@link AssociationSqlBuilder}
 */
@SuppressWarnings("java:S1133")
@Deprecated
public class AssociativeSqlBuilder {

    public static final String WHERE = " WHERE ";
    public static final String IN_S = " IN (%s)";
    public static final String DELETE_FROM = "DELETE FROM ";

    private final String deallocateFormat;
    private final String count;
    private final String insert;
    private final String insertPlaceholders;
    private final boolean insertCreateUser;

    @Getter
    private final String getByLeftId;
    @Getter
    private final String deleteByLeftId;
    @Getter
    private final String getByRightId;
    @Getter
    private final String deleteByRightId;

    public AssociativeSqlBuilder(String table, String left, String right, String createUserColumn) {
        deallocateFormat = DELETE_FROM + table + WHERE + left + IN_S + " AND " + right + IN_S;
        count = "SELECT COUNT(*) FROM " + table + WHERE + left + IN_S + " AND " + right + IN_S;
        if (createUserColumn == null) {
            insert = "INSERT INTO " + table + " (" + left + ", " + right + ") values (?, ?)";
            insertPlaceholders = ", (?, ?)";
            insertCreateUser = false;
        } else {
            insert = "INSERT INTO " + table + " (" + left + ", " + right + ", " + createUserColumn  + ") values (?, ?, ?)";
            insertPlaceholders = ", (?, ?, ?)";
            insertCreateUser = true;
        }

        getByLeftId = "SELECT " + right + " FROM " + table + WHERE + left + " = ?";
        deleteByLeftId = DELETE_FROM + table + WHERE + left + " = ?";
        getByRightId = "SELECT " + left + " FROM " + table + WHERE + right + " = ?";
        deleteByRightId = DELETE_FROM + table + WHERE + right + " = ?";
    }

    private String generatePlaceHolders(int size) {
        return "?" + IntStream.range(1, size).mapToObj(i -> ", ?").collect(Collectors.joining());
    }

    private List<Object> unionArgs(Object[] leftIds, Object[] rightIds) {
        List<Object> argList = new ArrayList<>(leftIds.length + rightIds.length);
        Collections.addAll(argList, leftIds);
        Collections.addAll(argList, rightIds);
        return argList;
    }

    public SqlAndArgs buildDeallocate(Object[] leftIds, Object[] rightIds) {
        String sql = String.format(deallocateFormat, generatePlaceHolders(leftIds.length), generatePlaceHolders(rightIds.length));
        return new SqlAndArgs(sql, unionArgs(leftIds, rightIds));
    }

    public SqlAndArgs buildCount(Object[] leftIds, Object[] rightIds) {
        String sql = String.format(count, generatePlaceHolders(leftIds.length), generatePlaceHolders(rightIds.length));
        return new SqlAndArgs(sql, unionArgs(leftIds, rightIds));
    }

    public SqlAndArgs buildAllocate(Collection<?> leftIds, Collection<?> rightIds, Object userId) {
        StringBuilder sql = new StringBuilder(insert);
        int totalSize = leftIds.size() * rightIds.size();
        for (int i = 0; i < totalSize - 1; i++) {
            sql.append(insertPlaceholders);
        }

        List<Object> args = new ArrayList<>(totalSize * (insertCreateUser ? 3 : 2));
        for (Object leftId : leftIds) {
            for (Object rightId : rightIds) {
                args.add(leftId);
                args.add(rightId);
                if (insertCreateUser) {
                    args.add(userId);
                }
            }
        }
        return new SqlAndArgs(sql.toString(), args);
    }

}
