package ml.calumma.web.web.repository.core.symbol;

public enum SearchOperation {

    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, IN;

    public static final String[] SIMPLE_OPERATION_SET = { "eq", "ne", "gt", "lt", "like" };

    public static SearchOperation getSimpleOperation(final String input)
    {
        switch (input) {
            case "eq": return EQUALITY;
            case "ne": return NEGATION;
            case "gt": return GREATER_THAN;
            case "lt": return LESS_THAN;
            case "like": return LIKE;
            case "in": return IN;
            default: return null;
        }
    }
}
