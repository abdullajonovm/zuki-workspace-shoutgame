package com.meinc.ergo.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MissingRequiredParamExceptionMapper 
implements ExceptionMapper<MissingRequiredParamException>
{
    @Override
    public Response toResponse(MissingRequiredParamException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
