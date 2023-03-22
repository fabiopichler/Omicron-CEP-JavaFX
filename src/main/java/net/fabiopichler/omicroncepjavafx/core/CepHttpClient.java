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

package net.fabiopichler.omicroncepjavafx.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CepHttpClient implements Runnable {
    private final String cepValue;
    private final CepResponseEventHandler onResponse;
    private final CepErrorEventHandler onError;

    public CepHttpClient(String cepValue, CepResponseEventHandler onResponse, CepErrorEventHandler onError) {
        this.cepValue = cepValue;
        this.onResponse = onResponse;
        this.onError = onError;
    }

    @Override
    public void run() {
        try {
            connect();

        } catch (CEPNotFoundException e){
            if (onError != null)
                onError.call(404);

        } catch (Exception e) {
            if (onError != null)
                onError.call(-1);
        }
    }

    private void connect() throws IOException {
        URL url = new URL("https://viacep.com.br/ws/" + cepValue + "/json/");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int responseCode = conn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (onError != null)
                onError.call(responseCode);

            return;
        }

        try {
            Cep cep = getCep(conn.getInputStream());

            if (onResponse != null)
                onResponse.call(cep);

        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static Cep getCep(InputStream inputStream) {
        JSONTokener tokener = new JSONTokener(inputStream);
        JSONObject root = new JSONObject(tokener);

        if (root.has("erro") && root.getBoolean("erro"))
            throw new CEPNotFoundException("CEP not Found");

        var cep = new Cep();

        cep.cep = root.getString("cep");
        cep.logradouro = root.getString("logradouro");
        cep.complemento = root.getString("complemento");
        cep.bairro = root.getString("bairro");
        cep.localidade = root.getString("localidade");
        cep.uf = root.getString("uf");
        cep.ibge = root.getString("ibge");
        cep.gia = root.getString("gia");
        cep.ddd = root.getString("ddd");
        cep.siafi = root.getString("siafi");

        return cep;
    }
}
