package com.meinc.bus.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.config.TaskNamespaceHandler;
import org.springframework.transaction.config.TxNamespaceHandler;

import com.meinc.mrsoa.service.exception.AdaptorException;

public class SpringAssemblerPluginFactory {
  private static Log log = LogFactory.getLog(SpringAssemblerPluginFactory.class);
	private static Map<String,ApplicationContext> springApplicationContexts = new HashMap<String,ApplicationContext>();
	
    public static IAssemblerPlugin getInstance(String serviceName, String serviceNamespace,
                                               List<String> serviceInterfaces, String serviceVersion,
                                               Map<String,String> pluginParms) {
        String annotationClassesString = pluginParms.get("spring.annotation.classes");
        String[] annotationClasses = null;
        if (annotationClassesString != null)
            annotationClasses = annotationClassesString.trim().split(",");
        
        String scanPackagesString = pluginParms.get("spring.annotation.scan.packages");
        String[] scanPackages = null;
        if (scanPackagesString != null)
            scanPackages = scanPackagesString.trim().split(",");
        
	    String contextPath = pluginParms.get("spring.context.path");
	    
        if (   !(contextPath != null ^ scanPackages != null ^ annotationClasses != null) ||
                (contextPath != null && scanPackages != null && annotationClasses != null)   )
            throw new AdaptorException("Exactly one dependency parameter of 'spring.context.path', 'spring.annotation.scan.packages', or 'spring.annotation.classes' must be specified in the project's Spring-Assembler-Plugin Maven dependency properties.");
	    
	    String beanName = pluginParms.get("spring.bean.name");
	    if (beanName == null || beanName.length() == 0)
	        throw new AdaptorException("Required dependency parameter 'spring.bean.name' missing.  Please add it to the project's Spring-Assembler-Plugin Maven dependency properties.");

	    synchronized (springApplicationContexts) {
	        ApplicationContext springContext = null;
	        if (contextPath != null) {
	            // Only one plugin per application context file
                springContext = springApplicationContexts.get(contextPath);
	            if (springContext != null) {
	                log.info("Found pre-existing spring context for " + contextPath);
	            } else {
                    log.info("Loading new spring context for " + contextPath);
    	            try {
    	                GenericApplicationContext ctx = new GenericApplicationContext();
    	                XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
    	                xmlReader.setNamespaceHandlerResolver(new MrSoaSpringNamespaceHandlerResolver());
    	                Resource springContextResource = new DefaultResourceLoader().getResource(contextPath);
    	                xmlReader.loadBeanDefinitions(springContextResource);
    	                ctx.refresh();
    	                springContext = ctx;
    	            } catch (Throwable t) {
    	                log.error("Error while creating Spring Assembler Plugin for Spring context " + contextPath, t);
    	                return null;
    	            }
    	            springApplicationContexts.put(contextPath, springContext);
	            }
	        } else if (scanPackages != null) {
                for (String scanPackage : scanPackages)
                    if (springApplicationContexts.containsKey(scanPackage)) {
                        springContext = springApplicationContexts.get(scanPackage);
                        break;
                    }
                if (springContext == null) {
    	            try {
    	                log.info("Loading new spring context for scan paths " + scanPackagesString);
    	                //@SuppressWarnings("resource")
                        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(scanPackages);
    	                springContext = ctx;
    	            } catch (Throwable t) {
    	                log.error("Error while creating Spring Assembler Plugin for Spring scan paths " + scanPackagesString, t);
    	                return null;
    	            }
    	            for (String scanPackage : scanPackages)
    	                springApplicationContexts.put(scanPackage, springContext);
                }
	        } else if (annotationClasses != null) {
	            //synchronize on this global object because spring doesn't appear to be thread safe here and sometimes the streams get crossed when deploying services
	            synchronized (AnnotationConfigApplicationContext.class) {
	                for (String annotationClass : annotationClasses)
	                    if (springApplicationContexts.containsKey(annotationClass)) {
	                        springContext = springApplicationContexts.get(annotationClass);
	                        break;
	                    }
	                if (springContext == null) {
	                    try {
	                        log.info("Loading new spring context for annotation classes " + annotationClassesString);
	                        //@SuppressWarnings("resource")
	                        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
	                        for (String annotationClass : annotationClasses) {
	                            Class<?> clazz = Class.forName(annotationClass);
	                            ctx.register(clazz);
	                        }
	                        ctx.refresh();
	                        springContext = ctx;
	                    } catch (Throwable t) {
	                        log.error("Error while creating Spring Assembler Plugin for Spring annotation classes" + annotationClassesString, t);
	                        return null;
	                    }
	                    for (String annotationClass : annotationClasses)
	                        springApplicationContexts.put(annotationClass, springContext);
	                }
	            }
	        }

	        if (springContext == null)
	            throw new AdaptorException("Failed to generate Spring context");
	        
	        return new SpringAssemblerPlugin(springContext, beanName);
	    }
	}
	
    private static class MrSoaSpringNamespaceHandlerResolver extends DefaultNamespaceHandlerResolver {
        @Override
        public NamespaceHandler resolve(String namespaceUri) {
            if ("http://www.springframework.org/schema/tx".equals(namespaceUri)) {
                TxNamespaceHandler txHandler = new TxNamespaceHandler();
                txHandler.init();
                return txHandler;
            } else if ("http://www.springframework.org/schema/task".equals(namespaceUri)) {
                TaskNamespaceHandler taskHandler = new TaskNamespaceHandler();
                taskHandler.init();
                return taskHandler;
            }
            return super.resolve(namespaceUri);
        }
    }
}
