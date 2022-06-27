package com.meinc.bus.plugin;

import org.springframework.context.ApplicationContext;

public class SpringAssemblerPlugin implements IAssemblerPlugin {
    private ApplicationContext springApplicationContext;
    private String springBeanName;
    
    public SpringAssemblerPlugin(ApplicationContext springApplicationContext, String springBeanName) {
        this.springApplicationContext = springApplicationContext;
        this.springBeanName = springBeanName;
    }

    @Override
    public Object initService() {
        return springApplicationContext.getBean(springBeanName);
    }
}
