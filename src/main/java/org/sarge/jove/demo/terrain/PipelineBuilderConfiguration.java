package org.sarge.jove.demo.terrain;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.pipeline.Shader;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PipelineBuilderConfiguration {
//	private final LogicalDevice dev;
//	private final ResourceLoaderAdapter<InputStream, Shader> loader;
	private final Pipeline.Builder builder = new Pipeline.Builder();
	private final VkSpecializationInfo constants = Shader.constants(Map.of(0, 20f, 1, 2.5f));

//	public PipelineBuilderConfiguration(LogicalDevice dev, DataSource classpath) {
//		this.dev = dev;
//		this.loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));
//	}
//
//	@Bean
//	Shader vertex() throws IOException {
//		return loader.load("terrain.vert.spiv");
//	}
//
//	@Bean
//	Shader fragment() throws IOException {
//		return loader.load("terrain.frag.spiv");
//	}
//
//	@Bean
//	Shader control() throws IOException {
//		return loader.load("terrain.tesc.spiv");
//	}
//
//	@Bean
//	Shader evaluation() throws IOException {
//		return loader.load("terrain.tese.spiv");
//	}
//
//	@Bean
//	PipelineLayout pipelineLayout(DescriptorLayout layout) {
//		return new PipelineLayout.Builder()
//				.add(layout)
//				.add(new PushConstantRange(0, 3 * Matrix.IDENTITY.length(), Set.of(VkShaderStage.TESSELLATION_CONTROL, VkShaderStage.TESSELLATION_EVALUATION)))
//				.build(dev);
//	}
//
//	@Bean
//	PipelineCache cache(ApplicationConfiguration cfg) throws IOException {
//		// Init cache folder
//		final Path home = Paths.get(System.getProperty("user.home"));
//		final Path path = home.resolve("JOVE").resolve(cfg.getTitle());
//		Files.createDirectories(path);
//
//		// Load cache
//		final DataSource src = PipelineCache.Loader.source(path);
//		final var loader = new ResourceLoaderAdapter<>(src, new PipelineCache.Loader(dev));
//		return loader.load("pipeline.cache");
//	}

	@Autowired
	void init(PipelineLayout pipelineLayout, RenderPass pass) {
		builder
				.layout(pipelineLayout)
				.pass(pass)
				.allowDerivatives()
				.depth()
					.enable(true)
					.build();
	}

	@Autowired
	void init(Swapchain swapchain) {
		builder
				.viewport()
				.viewportAndScissor(new Rectangle(swapchain.extents()));
	}

	@Autowired
	void init(Shader control, Shader evaluation, Shader vertex, Shader fragment) {
		builder
				.shader(VkShaderStage.TESSELLATION_CONTROL)
					.shader(control)
					.constants(constants)
					.build()
				.shader(VkShaderStage.TESSELLATION_EVALUATION)
					.shader(evaluation)
					.constants(constants)
					.build()
				.tesselation()
					.points(4)		// TODO - from where?
					.build()
				.shader(VkShaderStage.VERTEX)
					.shader(vertex)
					.build()
				.shader(VkShaderStage.FRAGMENT)
					.shader(fragment)
					.build();
	}

	@Autowired
	void init(Model model) {
		builder
			.input()
				.add(model.layout())
				.build()
			.assembly()
				.topology(model.primitive())
				.build();
	}

	@PostConstruct
	public void init() {
		System.out.println();

//		pipelines = Pipeline.Builder.build(null, null, dev)
	}

	@Bean
	Pipeline.Builder builder() {
		return builder
				.derive()
				.rasterizer()
					.polygon(VkPolygonMode.LINE)
					.build();
	}

//	//@Autowired
//	@Bean
//	public List<Pipeline> init(PipelineCache cache) {
//		return Pipeline.Builder.build(List.of(builder), cache, dev);
//	}
}

/*
	//@Bean
	public List<Pipeline> pipelines(PipelineLayout pipelineLayout, PipelineCache cache, RenderPass pass, Swapchain swapchain, Shader control, Shader evaluation, Shader vertex, Shader fragment, Model model) {
		// Init main pipeline
		final var pipeline = new Pipeline.Builder()
				.layout(pipelineLayout)
				.pass(pass)
				.allowDerivatives()
				.viewport()
					.viewportScissor(new Rectangle(swapchain.extents()))
					.build()
				.shader(VkShaderStage.TESSELLATION_CONTROL)
					.shader(control)
					.constants(constants)
					.build()
				.shader(VkShaderStage.TESSELLATION_EVALUATION)
					.shader(evaluation)
					.constants(constants)
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
		return Pipeline.Builder.build(List.of(pipeline, wireframe), null cache, dev);
	}
*/
