package org.sarge.jove.demo.terrain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.io.ResourceLoaderAdapter;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.PushConstantRange;
import org.sarge.jove.platform.vulkan.pipeline.Shader;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PipelineConfiguration {
	private final LogicalDevice dev;
	private final ResourceLoaderAdapter<InputStream, Shader> loader;

	public PipelineConfiguration(LogicalDevice dev, DataSource classpath) {
		this.dev = dev;
		this.loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));
	}

	@Bean
	Shader vertex() throws IOException {
//		final var loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));		// TODO
		return loader.load("terrain.vert.spiv");
	}

	@Bean
	Shader fragment() throws IOException {
//		final var loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));		// TODO
		return loader.load("terrain.frag.spiv");
	}

	@Bean
	Shader control() throws IOException {
//		final var loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));		// TODO
		return loader.load("terrain.tesc.spiv");
	}

	@Bean
	PipelineLayout pipelineLayout(DescriptorLayout layout) {
		return new PipelineLayout.Builder()
				.add(layout)
				.add(new PushConstantRange(0, 3 * Matrix.IDENTITY.length(), Set.of(VkShaderStage.VERTEX)))
				.build(dev);
	}

	@Bean
	public Pipeline pipeline(RenderPass pass, Swapchain swapchain, Shader vertex, Shader fragment, PipelineLayout pipelineLayout, Model model) {
		return new Pipeline.Builder()
				.layout(pipelineLayout)
				.pass(pass)
				.viewport(new Rectangle(swapchain.extents()))
				.shader(VkShaderStage.VERTEX)
					.shader(vertex)
					.build()
				.shader(VkShaderStage.FRAGMENT)
					.shader(fragment)
					.build()
				.input()
					.add(model.layout())
					.build()
				.assembly()
					.topology(model.primitive())
					.build()
				.depth()
					.enable(true)
					.build()
				.build(dev);
	}
}
