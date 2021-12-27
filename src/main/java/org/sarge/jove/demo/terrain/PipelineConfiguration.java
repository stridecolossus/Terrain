package org.sarge.jove.demo.terrain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.io.ResourceLoaderAdapter;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.VkCullMode;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineCache;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.pipeline.PushConstantRange;
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
		return loader.load("terrain.vert.spiv");
	}

	@Bean
	Shader fragment() throws IOException {
		return loader.load("terrain.frag.spiv");
	}

	@Bean
	Shader control() throws IOException {
		return loader.load("terrain.tesc.spiv");
	}

	@Bean
	Shader evaluation() throws IOException {
		return loader.load("terrain.tese.spiv");
	}

	@Bean
	PipelineLayout pipelineLayout(DescriptorLayout layout) {
		return new PipelineLayout.Builder()
				.add(layout)
				.add(new PushConstantRange(0, 3 * Matrix.IDENTITY.length(), Set.of(VkShaderStage.TESSELLATION_EVALUATION)))
				.build(dev);
	}

	@Bean
	PipelineCache cache(ApplicationConfiguration cfg) throws IOException {
		// Init cache folder
		final Path home = Paths.get(System.getProperty("user.home"));
		final Path path = home.resolve("JOVE").resolve(cfg.getTitle());
		Files.createDirectories(path);

		// Load cache
		final DataSource src = PipelineCache.Loader.source(path);
		final var loader = new ResourceLoaderAdapter<>(src, new PipelineCache.Loader(dev));
		return loader.load("pipeline.cache");
	}

	@Bean
	public List<Pipeline> pipelines(PipelineLayout pipelineLayout, PipelineCache cache, RenderPass pass, Swapchain swapchain, Shader control, Shader evaluation, Shader vertex, Shader fragment, Model model) {
		// Init main pipeline
		final var pipeline = new Pipeline.Builder()
				.layout(pipelineLayout)
				.pass(pass)
				.allowDerivatives()
				.viewport(new Rectangle(swapchain.extents()))
				.shader(VkShaderStage.TESSELLATION_CONTROL)
					.shader(control)
					.build()
				.shader(VkShaderStage.TESSELLATION_EVALUATION)
					.shader(evaluation)
					.build()
				.tesselation()
					.points(4)		// TODO - from where?
					.build()
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
				.rasterizer()
					.polygon(VkPolygonMode.LINE)
					.cull(VkCullMode.BACK)
					.build()
				.depth()
					.enable(true)
					.build();

//		// Init wireframe pipeline
//		final var wireframe = pipeline
//				.derive()
//				.rasterizer()
//					.polygon(VkPolygonMode.LINE)
//					.build();

		// Build pipelines
		return Pipeline.Builder.build(List.of(pipeline/*, wireframe*/), null /*cache*/, dev);
	}
}
