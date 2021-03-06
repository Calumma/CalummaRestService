package ml.calumma.rest.repository.core.projections;

import ml.calumma.model.entity.CalummaEntity;
import ml.calumma.rest.repository.core.symbol.ProjectionField;
import ml.calumma.rest.repository.core.symbol.ProjectionType;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoreEntityProjectionBuilder<Entity extends CalummaEntity> {

    private final List<ProjectionField> projections;
    private final Class rootEntity;
    private final CriteriaBuilder criteriaBuilder;
    private Root<Entity> root;

    public CoreEntityProjectionBuilder(Root<Entity> root, Class rootEntity, CriteriaBuilder criteriaBuilder) {
        projections = new ArrayList<>();
        this.rootEntity = rootEntity;
        this.root = root;
        this.criteriaBuilder = criteriaBuilder;
    }

    public final CoreEntityProjectionBuilder<Entity> with(final ProjectionField projectionField) {
        projections.add(projectionField);
        return this;
    }

    public List<Selection> build() throws NoSuchFieldException, IllegalAccessException {

        List<Selection> selections = new ArrayList<>();
        CoreEntityProjection<Entity> projectionMaker = new CoreEntityProjection<>(root, rootEntity, criteriaBuilder);

        List<ProjectionField> notEagerProjections = projections.stream()
                .filter(x -> x.getProjectionType() != ProjectionType.EAGER)
                .collect(Collectors.toList());

        if(notEagerProjections.size() > 0) {
            for(ProjectionField projection: notEagerProjections){
                selections.add(projectionMaker.toSelection(projection));
            }
        }

        return selections;
    }

    public List<ProjectionField> getEagerSelections() throws NoSuchFieldException {
        return  projections.stream()
                .filter(x -> x.getProjectionType() == ProjectionType.EAGER)
                .collect(Collectors.toList());
    }
}
