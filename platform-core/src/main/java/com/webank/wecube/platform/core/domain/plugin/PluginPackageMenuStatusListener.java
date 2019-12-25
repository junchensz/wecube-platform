package com.webank.wecube.platform.core.domain.plugin;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu.Status;
import com.webank.wecube.platform.core.jpa.PluginPackageMenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu.Status.ACTIVE;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu.Status.INACTIVE;

public class PluginPackageMenuStatusListener implements ApplicationContextAware {
    private PluginPackageMenuRepository packageMenuRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ApplicationContext applicationContext;

    @PrePersist
    public void prePersist(PluginInstance pluginInstance){
        updatePluginPackageMenuStatusForPluginPackage(pluginInstance, INACTIVE, ACTIVE);
    }

    @PreRemove
    public void preRemove(PluginInstance pluginInstance) {
        updatePluginPackageMenuStatusForPluginPackage(pluginInstance, ACTIVE, INACTIVE);
    }

    @PostLoad
    public void postLoad(PluginInstance pluginInstance) {
        updatePluginPackageMenuStatusForPluginPackage(pluginInstance, INACTIVE, ACTIVE);
    }

    private void updatePluginPackageMenuStatusForPluginPackage(PluginInstance pluginInstance, Status fromStatus, Status toStatus) {
        if (null == packageMenuRepository) {
            this.packageMenuRepository = applicationContext.getBean(PluginPackageMenuRepository.class);
        }
        String packageId = pluginInstance.getPluginPackage().getId();
        Optional<List<PluginPackageMenu>> pluginPackageMenusOptional = packageMenuRepository.findAllMenusByStatusAndPluginPackageId(fromStatus, packageId);
        if (pluginPackageMenusOptional.isPresent()) {
            List<PluginPackageMenu> pluginPackageMenus = pluginPackageMenusOptional.get();
            if (pluginPackageMenus.size() > 0) {
                List<PluginPackageMenu> updatePluginPackageMenus = new ArrayList<>();
                pluginPackageMenus.forEach(
                        pluginPackageMenu -> {
                            logger.info("Updating PluginPackageMenu[{}] to {}", pluginPackageMenu.getId(), toStatus);
                            pluginPackageMenu.setStatus(toStatus);
                            updatePluginPackageMenus.add(pluginPackageMenu);
                        }
                );
                if (updatePluginPackageMenus.size() > 0) {
                    packageMenuRepository.saveAll(updatePluginPackageMenus);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
