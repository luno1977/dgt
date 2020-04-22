package de.luno1977.dgt.analyzer.view;

import com.google.common.collect.ImmutableBiMap;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Map;

public class ChessBoardView extends FlexLayout {

    public static final int NUMBER_OF_ROWS = 8;
    public static final int NUMBER_OF_COLS = 8;

    private HorizontalLayout self = new HorizontalLayout();
    private SquareView[][] squares = new SquareView[NUMBER_OF_ROWS][NUMBER_OF_COLS];

    private static Map<Character, String> fenToImage = ImmutableBiMap.<Character, String>builder()
            .put('P', "pieces/Chess_plt45.svg")
            .put('p', "pieces/Chess_pdt45.svg")

            .put('R', "pieces/Chess_rlt45.svg")
            .put('r', "pieces/Chess_rdt45.svg")

            .put('K', "pieces/Chess_klt45.svg")
            .put('k', "pieces/Chess_kdt45.svg")

            .put('Q', "pieces/Chess_qlt45.svg")
            .put('q', "pieces/Chess_qdt45.svg")

            .put('N', "pieces/Chess_nlt45.svg")
            .put('n', "pieces/Chess_ndt45.svg")

            .put('B', "pieces/Chess_blt45.svg")
            .put('b', "pieces/Chess_bdt45.svg")
            .build();

    public ChessBoardView() {
        super();
        this.setPlain(self);
        self.addClassName("board");
        self.setWidth("400px");
        self.setHeight("400px");

        for (int r = 0; r < NUMBER_OF_COLS; r++) {
            VerticalLayout rowLayout = new VerticalLayout();
            setPlain(rowLayout);
            for (int c = 0; c < NUMBER_OF_COLS; c++) {
                squares[r][c] = new SquareView( (r+c)%2 == 1 );
                rowLayout.add(squares[r][c]);
            }
            self.add(rowLayout);
        }

        add(self);

        present("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

    }

    public void present(String fenPeacePlacement) {
        int row = 0; int column = 0;
        for (int i = 0; i < fenPeacePlacement.length(); i++) {
            char c = fenPeacePlacement.charAt(i);
            if (c >= 65 && c <= 122) {
                String imagePath = fenToImage.get(c);
                if (imagePath != null) {
                    Image image = new Image(imagePath, "");
                    image.setWidthFull();
                    squares[column][row].add(image);
                } else {
                    throw new RuntimeException("Peace '" + c + "' not found.");
                }
                column = column + 1;
            } else if (c == '/') {
                row = row + 1;
                column = 0;
            } else if (c >= 48 && c <= 57) {
                int free = Integer.parseInt(String.valueOf(c));
                for (int j = 0; j < free; j++) {
                    squares[column++][row].removeAll();
                }
            }
        }
    }

    private void setPlain(ThemableLayout layout) {
        layout.setMargin(false); layout.setSpacing(false); layout.setPadding(false);
    }

    private static class SquareView extends Span {
        final boolean dark;

        SquareView(boolean dark) {
            this.dark = dark;
            this.setSizeFull();
            if (dark) {
                this.getStyle().set("background-color", "STEELBLUE");
            } else {
                this.getStyle().set("background-color", "POWDERBLUE");
            }
            this.getStyle().set("display", "flex");
        }
    }
}
