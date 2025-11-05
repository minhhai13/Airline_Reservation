package com.airline.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global Exception Handler Bắt tất cả các exception từ @Controller và
 * @RestController và chuyển hướng đến các trang error.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi 404 (Không tìm thấy Handler/Endpoint) Được kích hoạt khi
     * throwExceptionIfNoHandlerFound=true trong dispatcher-servlet.xml
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/404"); // Trỏ đến /WEB-INF/views/error/404.html
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        return mav;
    }

    /**
     * Xử lý các lỗi nghiệp vụ (ví dụ: tìm không thấy flight, hết vé, v.v.) Đây
     * là những lỗi 500 (Internal Server Error) do logic nghiệp vụ. Chúng ta bắt
     * các lỗi này từ các controller (ví dụ: IllegalArgumentException).
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleBusinessException(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500"); // Trỏ đến /WEB-INF/views/error/500.html
        // Hiển thị thông báo lỗi cụ thể
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("exception", ex);
        return mav;
    }

    /**
     * Xử lý tất cả các lỗi 500 (Internal Server Error) khác Đây là "catch-all"
     * cho mọi exception không được xử lý cụ thể
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllOtherExceptions(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500"); // Trỏ đến /WEB-INF/views/error/500.html
        mav.addObject("errorMessage", "Đã có lỗi máy chủ xảy ra. Vui lòng thử lại sau.");
        mav.addObject("exception", ex);
        return mav;
    }
}
