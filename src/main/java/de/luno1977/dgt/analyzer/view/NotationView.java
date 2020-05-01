package de.luno1977.dgt.analyzer.view;

import com.vaadin.flow.component.html.Div;

public class NotationView extends Div {

    public NotationView() {
        getElement().getStyle().set("background", "var(--lumo-contrast-10pct)");
        getElement().getStyle().set("border-radius", "var(--lumo-border-radius)");
        getElement().getStyle().set("overflow", "auto");
        setSizeFull();

        Div testElement = null;
        for (int i = 1; i < 200; i++) {
            testElement = new Div();
            testElement.getStyle().set("display", "inline-block");
            testElement.getStyle().set("width", "fit-content");
            testElement.getStyle().set("padding", "1px");
            testElement.getStyle().set("margin-left", "2px");
            testElement.getStyle().set("margin-top", "2px");
            testElement.getStyle().set("border-radius", "var(--lumo-border-radius)");
            testElement.setSizeUndefined();
            testElement.add("Na" + i);
            testElement.getStyle().set("background", "var(--lumo-contrast-10pct)");
            add(testElement);
        }

        testElement.getStyle().set("background", "var(--lumo-contrast-30pct)");
    }
}
