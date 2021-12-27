package org.sarge.jove.demo.terrain;

import java.io.IOException;

import org.sarge.jove.io.DataSource;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ResourceLoaderAdapter;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.ImageCopyCommand;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor;
import org.sarge.jove.platform.vulkan.image.Sampler;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.image.VulkanImageLoader;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextureConfiguration {
	@Autowired private LogicalDevice dev;

	@Bean
	public Sampler sampler() { // ApplicationConfiguration cfg) {
		return new Sampler.Builder()
				//.anisotropy(cfg.getAnisotropy())
				//.wrap(Sampler.Wrap.EDGE.mode(false))
//				.wrap(VkSamplerAddressMode.MIRRORED_REPEAT)
				.wrap(VkSamplerAddressMode.CLAMP_TO_EDGE)
				.build(dev);
	}

	@Bean
	public static ImageData heightmap(DataSource data) {
		// Load texture image
//		final var loader = new ResourceLoaderAdapter<>(data, new NativeImageLoader());
//		final ImageData image = loader.load("heightmap-grayscale.jpg");
		final var loader = new ResourceLoaderAdapter<>(data, new VulkanImageLoader());
		return loader.load("heightmap.ktx2");
	}

	@Bean
	public View texture(ImageData image, AllocationService allocator, Pool transfer) throws IOException {
		// Determine image format
		final VkFormat format = VkFormat.R16_UNORM; // TODO - INT -> NORM if one channel, should we ALWAYS infer the format and ignore (remove?) the hint?
//		final VkFormat format = FormatBuilder.format(image);
//		System.out.println(format);

		// Create descriptor
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.type(VkImageType.TWO_D)
				.aspect(VkImageAspect.COLOR)
				.extents(image.extents())
				.format(format)
				.mipLevels(image.levels().size())
				.build();

		// Init image memory properties
		final var props = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.TRANSFER_DST)
				.usage(VkImageUsageFlag.SAMPLED)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		// Create texture
		final Image texture = new Image.Builder()
				.descriptor(descriptor)
				.properties(props)
				.build(dev, allocator);

		// Prepare texture
		new Barrier.Builder()
				.source(VkPipelineStage.TOP_OF_PIPE)
				.destination(VkPipelineStage.TRANSFER)
				.barrier(texture)
					.newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.destination(VkAccess.TRANSFER_WRITE)
					.build()
				.build()
				.submitAndWait(transfer);

		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, image.data());

		// Copy staging to image
		new ImageCopyCommand.Builder()
				.image(texture)
				.buffer(staging)
				.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.region(image)
				.build()
				.submitAndWait(transfer);

		// Release staging
		staging.destroy();

		// Transition to sampled image
		new Barrier.Builder()
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.VERTEX_SHADER)
				.barrier(texture)
					.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
					.source(VkAccess.TRANSFER_WRITE)
					.destination(VkAccess.SHADER_READ)
					.build()
				.build()
				.submitAndWait(transfer);

		// Create texture view
		return View.of(texture);
	}
}
