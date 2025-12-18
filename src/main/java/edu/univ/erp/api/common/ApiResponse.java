package edu.univ.erp.api.common;

public class ApiResponse<T>{

    private boolean success; private String message; private T data;
    
    public ApiResponse(boolean s,String m,T d){success=s;message=m;data=d;}
    public static <T> ApiResponse<T> success(String m,T d){return new ApiResponse<>(true,m,d);}
    public static <T> ApiResponse<T> success(String m){return new ApiResponse<>(true,m,null);}
    public static <T> ApiResponse<T> fail(String m){return new ApiResponse<>(false,m,null);}
    public boolean isSuccess(){return success;} public String getMessage(){return message;} public T getData(){return data;}
}