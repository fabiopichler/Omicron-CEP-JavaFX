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
import net.fabiopichler.omicroncepjavafx.core.Database;
import net.fabiopichler.omicroncepjavafx.core.CepHttpClient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.concurrent.locks.ReentrantLock;

public class AppController {

    private final ReentrantLock mutex = new ReentrantLock();

    @FXML
    private Label cepLabel;
    @FXML
    private Label logradouroLabel;
    @FXML
    private Label bairroLabel;
    @FXML
    private Label localidadeLabel;
    @FXML
    private Label ufLabel;
    @FXML
    private TextField cepTextField;
    @FXML
    private ListView<Cep> listView;
    private ObservableList<Cep> data;

    private boolean blockRequest = false;

    @FXML
    private void initialize() {
        HBox.setHgrow(cepTextField, Priority.ALWAYS);

        initListView();
    }

    private void initListView() {
        data = FXCollections.observableArrayList();

        Database.get().readCeps(data);

        listView.setCellFactory(new CepCellFactory(this::onDeleteCep));
        listView.setItems(data);
    }

    @FXML
    protected void onTextFieldAction(ActionEvent e) {
        requestCep();
    }

    @FXML
    protected void onSearchButtonClick() {
        requestCep();
    }

    private void onCepResponse(Cep cep) {
        try {
            mutex.lock();

            Platform.runLater(() -> {
                cepLabel.setText(cep.cep);
                logradouroLabel.setText(cep.logradouro);
                bairroLabel.setText(cep.bairro);
                localidadeLabel.setText(cep.localidade);
                ufLabel.setText(cep.uf);

                Database.get().setCep(cep);
                data.clear();
                Database.get().readCeps(data);
            });

        } finally {
            blockRequest = false;
            mutex.unlock();
        }
    }

    private void onCepError(int code) {
        try {
            mutex.lock();

            Platform.runLater(() -> cepLabel.setText("Erro! Código: " + code));

        } finally {
            blockRequest = false;
            mutex.unlock();
        }
    }

    private void requestCep() {
        try {
            mutex.lock();

            String cep = cepTextField.getText();

            if (blockRequest || !cep.matches("^\\d{5}-{0,1}\\d{3}$"))
                return;

            blockRequest = true;

            cepLabel.setText("Carregando...");
            logradouroLabel.setText("___");
            bairroLabel.setText("___");
            localidadeLabel.setText("___");
            ufLabel.setText("___");

            var httpClient = new CepHttpClient(cep, this::onCepResponse, this::onCepError);
            new Thread(httpClient).start();

        } finally {
            mutex.unlock();
        }
    }

    private void onDeleteCep(String cepId) {
        Database.get().deleteCep(cepId);
        data.clear();
        Database.get().readCeps(data);
    }
}
