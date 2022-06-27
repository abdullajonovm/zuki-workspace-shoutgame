package com.meinc.ergo.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ExcelCreationExceptionMapper
implements ExceptionMapper<ExcelCreationException>
{
    @Override
    public Response toResponse(ExcelCreationException exception)
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

}
