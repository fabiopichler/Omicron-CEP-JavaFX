/*-------------------------------------------------------------------------------

Copyright (c) 2023 Fábio Pichler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-------------------------------------------------------------------------------*/

package net.fabiopichler.omicroncepjavafx.gui;

import net.fabiopichler.omicroncepjavafx.core.Cep;
import net.fabiopichler.omicroncepjavafx.core.CepDeleteEventHandler;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class CepListCell extends ListCell<Cep> {
    private final CepDeleteEventHandler onDelete;
    private final HBox contentBox;
    private final Label cepLabel = new Label();
    private final Label logradouroLabel = new Label();
    private final Label bairroLabel = new Label();
    private final Label localidadeLabel = new Label();
    private final Label ufLabel = new Label();

    private String cepId;

    public CepListCell(CepDeleteEventHandler onDelete) {
        super();

        this.onDelete = onDelete;

        cepLabel.getStyleClass().add("cep-title");
        logradouroLabel.getStyleClass().add("cep-content");
        bairroLabel.getStyleClass().add("cep-content");
        localidadeLabel.getStyleClass().add("cep-content");
        ufLabel.getStyleClass().add("cep-content");

        var logradouroBox = new HBox(new Label("Logradouro: "), logradouroLabel);
        var bairroBox = new HBox(new Label("Bairro: "), bairroLabel);
        var localidadeBox = new HBox(new Label("Cidade: "), localidadeLabel, new Label("/"), ufLabel);

        var vBox = new VBox(cepLabel, logradouroBox, bairroBox, localidadeBox);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        var image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("delete.png")));
        var imageView = new ImageView(image);
        imageView.setFitHeight(14);
        imageView.setFitHeight(14);
        imageView.setPreserveRatio(true);

        var button = new Button();
        button.setGraphic(imageView);
        button.getStyleClass().add("button-delete");
        button.setOnAction(this::onDeleteHandle);

        contentBox = new HBox(vBox, button);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setSpacing(10);
    }

    @Override
    public void updateItem(Cep item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            cepId = null;
            setText(null);
            setGraphic(null);
        } else if (item != null) {
            cepId = item.cep;

            cepLabel.setText(item.cep);
            logradouroLabel.setText(item.logradouro);
            bairroLabel.setText(item.bairro);
            localidadeLabel.setText(item.localidade);
            ufLabel.setText(item.uf);

            setText(null);
            setGraphic(contentBox);
        } else {
            cepId = null;
            setText("null");
            setGraphic(null);
        }
    }

    private void onDeleteHandle(ActionEvent actionEvent) {
        if (onDelete != null && cepId != null)
            onDelete.call(cepId);
    }
}
