package org.sarge.jove.demo.terrain;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.model.GridBuilder;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Quad;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfiguration {
	@Autowired private LogicalDevice dev;
	@Autowired private AllocationService allocator;
	@Autowired private Pool transfer;

	@Bean
	public static Model model(ImageData heightmap) {
		final Dimensions size = new Dimensions(64, 64);
		return new GridBuilder()
				.size(size)
				.scale(0.25f)
				.primitive(Primitive.PATCH)
				.index(Quad.STRIP)
				.build();
	}

	@Bean
	public VulkanBuffer vbo(Model model) {
		return buffer(model.vertexBuffer(), VkBufferUsageFlag.VERTEX_BUFFER);
	}

	@Bean
	public VulkanBuffer index(Model model) {
		return buffer(model.indexBuffer(), VkBufferUsageFlag.INDEX_BUFFER);
	}

	protected VulkanBuffer buffer(Bufferable data, VkBufferUsageFlag usage) {
		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);

		// Init buffer memory properties
		final MemoryProperties<VkBufferUsageFlag> props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_DST)
				.usage(usage)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		// Create buffer
		final VulkanBuffer buffer = VulkanBuffer.create(dev, allocator, staging.length(), props);

		// Copy staging to buffer
		staging.copy(buffer).submitAndWait(transfer);

		// Release staging
		staging.destroy();

		return buffer;
	}
}
