package ml.calumma.web.web.repository.core.symbol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectionField {

    private String nameField;
    private ProjectionType projectionType;
    private String alias;

    public ProjectionField(final String field, String alias) {
        this.alias = alias;
        initProjection(field);
    }

    public ProjectionField(final String field) {
        initProjection(field);
    }

    private void initProjection(final String field){
        Matcher patternAvg = Pattern.compile("avg\\((.*?)\\)").matcher(field);
        Matcher patternSum = Pattern.compile("sum\\((.*?)\\)").matcher(field);
        Matcher patternCount = Pattern.compile("count\\((.*?)\\)").matcher(field);
        Matcher patternDistinctCount = Pattern.compile("distinct_count\\((.*?)\\)").matcher(field);
        Matcher min = Pattern.compile("min\\((.*?)\\)").matcher(field);
        Matcher max = Pattern.compile("max\\((.*?)\\)").matcher(field);
        Matcher eager = Pattern.compile("eager\\((.*?)\\)").matcher(field);

        if (patternAvg.find()) {
            nameField = patternAvg.group(1);
            projectionType = ProjectionType.AVERAGE;
        } else if (patternSum.find()) {
            nameField = patternSum.group(1);
            projectionType = ProjectionType.SUM;
        } else if (patternDistinctCount.find()) {
            nameField = patternDistinctCount.group(1);
            projectionType = ProjectionType.DISTINCT_COUNT;
        } else if (patternCount.find()) {
            nameField = patternCount.group(1);
            projectionType = ProjectionType.COUNT;
        }
        else if(eager.find()){
            nameField = eager.group(1);
            projectionType = ProjectionType.EAGER;
        }
        else if(min.find()){
            nameField = min.group(1);
            projectionType = ProjectionType.MIN;
        }
        else if(max.find()){
            nameField = max.group(1);
            projectionType = ProjectionType.MAX;
        }
        else {
            nameField = field;
            projectionType = ProjectionType.SIMPLE;
        }
    }

    public static boolean isAggregation(final String field) {

        Matcher patternAvg = Pattern.compile("avg\\((.*?)\\)").matcher(field);
        Matcher patternSum = Pattern.compile("sum\\((.*?)\\)").matcher(field);
        Matcher patternCount = Pattern.compile("count\\((.*?)\\)").matcher(field);
        Matcher patternDistinctCount = Pattern.compile("distinct_count\\((.*?)\\)").matcher(field);
        return patternAvg.find() || patternSum.find() || patternCount.find() || patternDistinctCount.find();
    }

    public String getNameField() {
        return nameField;
    }

    @JsonIgnore
    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setNameField(String nameField) {
        this.nameField = nameField;
    }


    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
