package com.meinc.ergo.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidParamExceptionMapper 
implements ExceptionMapper<InvalidParamException>
{
    @Override
    public Response toResponse(InvalidParamException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
