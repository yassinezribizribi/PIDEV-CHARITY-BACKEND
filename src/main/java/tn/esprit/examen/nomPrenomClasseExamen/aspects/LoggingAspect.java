package tn.esprit.examen.nomPrenomClasseExamen.aspects;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.BeanUtils;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;

import java.util.Arrays;

@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* tn.esprit.examen.nomPrenomClasseExamen.services.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = sanitizeArguments(joinPoint.getArgs());

        logger.info("➡ Entrée dans la méthode : {}", methodName);
        try {
            Object result = joinPoint.proceed();
            logger.info("⬅ Sortie de la méthode : {}", methodName);
            return result;
        } catch (Exception e) {
            logger.error("❌ Exception dans la méthode {} : ", methodName, e);
            throw e;
        }
    }

    private Object[] sanitizeArguments(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof TestimonialDTO) {
                        TestimonialDTO dto = new TestimonialDTO();
                        BeanUtils.copyProperties(arg, dto);
                        dto.setBeforePhotoBase64("[MASKED]");
                        dto.setAfterPhotoBase64("[MASKED]");
                        return dto;
                    }
                    return arg;
                })
                .toArray();
    }
}
