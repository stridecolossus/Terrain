package org.sarge.jove.demo.terrain;

import java.nio.ByteBuffer;

import org.sarge.jove.control.ActionBindings;
import org.sarge.jove.control.Button.ToggleHandler;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.platform.desktop.KeyboardDevice;
import org.sarge.jove.platform.desktop.MouseDevice;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.pipeline.PushConstantUpdateCommand;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.OrbitalCameraController;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.scene.RenderLoop;
import org.sarge.jove.scene.RenderLoop.Task;
import org.sarge.jove.util.BufferHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CameraConfiguration {
	private Matrix projection;
	private final Camera cam = new Camera();
	private final OrbitalCameraController controller;
//	private final MutableRotation rot = new MutableRotation(Vector.Y);

	public CameraConfiguration(Swapchain swapchain) {
		projection = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());
		controller = new OrbitalCameraController(cam, swapchain.extents());
		controller.radius(10);
		controller.scale(1f);
	}

	@Bean
	public ActionBindings bindings(Window window, RenderLoop loop, ToggleHandler toggle) {
		// Bind stop action
		final ActionBindings bindings = new ActionBindings();
		final KeyboardDevice keyboard = window.keyboard();
		keyboard.bind(bindings);
		bindings.bind(keyboard.key("ESCAPE"), loop::stop);

//		class PlayPauseAction implements ToggleHandler {
//			@Override
//			public void handle(boolean pressed) {
//				if(animator.isPlaying()) {
//					animator.apply(Player.State.PAUSE);
//				}
//				else {
//					animator.apply(Player.State.PLAY);
//				}
////				animator.apply(pressed ? Player.State.PAUSE : Player.State.PAUSE);
//			}
//		}
		bindings.bind(keyboard.key("SPACE"), toggle);

		// Bind camera controller
		final MouseDevice mouse = window.mouse();
		bindings.bind(mouse.pointer(), controller::update);
		bindings.bind(mouse.wheel(), controller::zoom);

		return bindings;
	}

	@Bean
	public static PushConstantUpdateCommand update(PipelineLayout layout) {
		return PushConstantUpdateCommand.of(layout);
	}

//	@Bean
//	Animator animator() {
//		final Animator animator = new Animator(Duration.ofSeconds(10), new RotationAnimation(rot));
//		animator.setRepeating(true);
//		//animator.apply(Player.State.PLAY);
//		return animator;
//	}

	@Bean
	public Task matrix(PushConstantUpdateCommand update) {
		// Init projection matrix
		final ByteBuffer data = update.data();
		BufferHelper.insert(2, projection, data);

		final Matrix model = Matrix.IDENTITY; // translation(new Vector(-8, 2, 0));

		// Update modelview matrix
		return () -> {
			data.rewind();
			model.buffer(data);
			cam.matrix().buffer(data);
////			final Matrix model = Rotation.matrix(rot);
		};
	}
}
