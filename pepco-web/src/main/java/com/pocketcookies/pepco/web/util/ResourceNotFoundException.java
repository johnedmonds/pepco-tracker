/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pocketcookies.pepco.web.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author jack
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
}
