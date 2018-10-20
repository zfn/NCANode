package kz.ncanode.api.version.v10;

import kz.ncanode.api.ApiServiceProvider;
import kz.ncanode.api.core.ApiArgument;
import kz.ncanode.api.core.ApiMethod;
import kz.ncanode.api.core.ApiStatus;
import kz.ncanode.api.core.ApiVersion;
import kz.ncanode.api.exceptions.ApiErrorException;
import kz.ncanode.api.exceptions.InvalidArgumentException;
import kz.ncanode.api.version.v10.methods.PKCS12Info;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

public class ApiVersion10 implements ApiVersion {

    private ApiServiceProvider man = null;

    private Hashtable<String, ApiMethod> methods = null;

    public ApiVersion10() {
        methods = new Hashtable<>();

        methods.put("PKCS12.info", new PKCS12Info(this, man));
    }

    @Override
    public void setApiManager(ApiServiceProvider apiManager) {
        man = apiManager;
    }

    @Override
    public JSONObject process(JSONObject request) {
        // route method
        // check arguments required
        // check arguments validate
        // run method
        // catch ApiError exception
        // process result

        String method = "";

        try {
            method = (String)request.get("method");
        } catch (ClassCastException e) {
            JSONObject resp = new JSONObject();
            resp.put("status", ApiStatus.STATUS_INVALID_PARAMETER);
            resp.put("message", "Invalid parameter \"method\"");
            return resp;
        }

        if (!methods.containsKey(method)) {
            JSONObject resp = new JSONObject();
            resp.put("status", ApiStatus.STATUS_METHOD_NOT_FOUND);
            resp.put("message", "Method not found");
            return resp;
        }

        JSONObject params;

        try {
            params = (JSONObject)request.get("params");
        } catch (ClassCastException e) {
            JSONObject resp = new JSONObject();
            resp.put("status", ApiStatus.STATUS_INVALID_PARAMETER);
            resp.put("message", "Invalid parameter \"params\"");
            return resp;
        }

        ApiMethod m = methods.get(method);


        // строим список аргументов
        ArrayList<ApiArgument> args = m.arguments();

        if (args != null && args.size() > 0) {

            if (params == null) {
                JSONObject resp = new JSONObject();
                resp.put("status", ApiStatus.STATUS_PARAMS_NOT_FOUND);
                resp.put("message", "\"params\" not found in request");
                return resp;
            }

            for (ApiArgument arg : args) {
                try {
                    arg.params = params;
                    arg.validate();
                } catch (InvalidArgumentException e) {
                    JSONObject resp = new JSONObject();
                    resp.put("status", ApiStatus.STATUS_INVALID_PARAMETER);
                    resp.put("message", "Invalid parameter \"" + arg.name() + "\"");
                    return resp;
                }
            }
        }

        m.args = args;

        JSONObject response;

        try {
            response = m.handle();
        } catch (ApiErrorException e) {
            JSONObject resp = new JSONObject();
            resp.put("status", ApiStatus.STATUS_API_ERROR);
            resp.put("message", "Api error: " + e.getMessage());
            return resp;
        }

        response.put("status", m.status);
        response.put("message", m.message);

        return response;
    }
}