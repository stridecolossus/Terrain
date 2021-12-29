package org.sarge.jove.demo.terrain;

import java.util.List;

import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.Sampler;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.render.Binding;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorPool;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescriptorConfiguration {
	@Autowired private LogicalDevice dev;
	@Autowired private ApplicationConfiguration cfg;

	private final Binding samplerBinding = new Binding.Builder()
			.binding(0)
			.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
			.stage(VkShaderStage.TESSELLATION_CONTROL)
			.stage(VkShaderStage.TESSELLATION_EVALUATION)
			.stage(VkShaderStage.FRAGMENT)
			.build();

	@Bean
	public DescriptorLayout layout() {
		return DescriptorLayout.create(dev, List.of(samplerBinding));
	}

	@Bean
	public DescriptorPool descriptorPool() {
		final int count = 2 * cfg.getFrameCount();
		return new DescriptorPool.Builder()
				.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, count)
				.max(count)
				.build(dev);
	}

	@Bean
	public List<DescriptorSet> descriptors(DescriptorPool pool, DescriptorLayout layout, Sampler sampler, View texture) {
		final List<DescriptorSet> descriptors = pool.allocate(layout, cfg.getFrameCount());
		DescriptorSet.set(descriptors, samplerBinding, sampler.resource(texture));
		DescriptorSet.update(dev, descriptors);
		return descriptors;
	}
}
