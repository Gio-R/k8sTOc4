package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Relationship;

public class C4RelationshipPresenter {
    public static String present(C4Relationship relationship) {
        StringBuilder sb = new StringBuilder();
        sb.append(PresenterUtils.sanitizeNamespacedId(relationship.getSource())).append(" -> ").append(PresenterUtils.sanitizeNamespacedId(relationship.getTarget()));
        if (!relationship.getTag().isBlank()) {
            sb.append(" ").append(relationship.getTag());
        }
        sb.append("\n");
        return sb.toString();
    }
}
