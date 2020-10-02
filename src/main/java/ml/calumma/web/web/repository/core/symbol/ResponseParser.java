package ml.calumma.web.web.repository.core.symbol;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseParser {

    private List<String> projectionFields;
    private final List<Object> rows;
    private List<Map<String, Object>> response;

    public ResponseParser(List<ProjectionField> projectionFields, List<Object> rows) {
        if(projectionFields != null && projectionFields.size() > 0) {
            this.projectionFields = new ArrayList<>();
            projectionFields.forEach(projection -> this.projectionFields.add(projection.getAlias()));
        }
        else
            this.projectionFields = null;
        this.rows = rows;
        this.response = new ArrayList<>();
    }

    public Page getFormattedResponse(Pageable pageable, long total) {
        if (projectionFields == null)
            return getFormattedResponseWithoutProjections(pageable, total);
        else
            return getFormattedResponseWithProjections(pageable, total);
    }

    public ResponseParser getFormattedResponseDictionary() {

        for (Object row : rows) {
            Map<String, Object> parsedRow = new HashMap<>();
            if (projectionFields.size() > 1) {
                for (int i = 0; i < projectionFields.size(); i++) {
                    parsedRow.put(projectionFields.get(i), ((Object[]) row)[i]);
                }
            } else {
                parsedRow.put(projectionFields.get(0), row);
            }
            response.add(parsedRow);
        }
        return this;
    }

    private Page getFormattedResponseWithProjections(Pageable pageable, long total) {
        Page pagedResponse = new PageImpl(response, pageable, total);
        return pagedResponse;
    }

    private Page getFormattedResponseWithoutProjections(Pageable pageable, long total) {
        return new PageImpl(rows, pageable, total);
    }

    public List<Map<String, Object>> getResponse() {
        return response;
    }

    public void setResponse(List<Map<String, Object>> response) {
        this.response = response;
    }
}
