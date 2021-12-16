package org.sarge.jove.demo.terrain;

import java.time.Duration;

import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.control.ActionBindings;
import org.sarge.jove.control.Animator;
import org.sarge.jove.control.Button.ToggleHandler;
import org.sarge.jove.control.Player;
import org.sarge.jove.control.RotationAnimation;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MutableRotation;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.desktop.KeyboardDevice;
import org.sarge.jove.platform.desktop.MouseDevice;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.pipeline.PushUpdateCommand;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.OrbitalCameraController;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.scene.RenderLoop;
import org.sarge.jove.scene.RenderLoop.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CameraConfiguration {
	private final Matrix projection;
	private final Camera cam = new Camera();
	private final OrbitalCameraController controller;
	private final MutableRotation rot = new MutableRotation(Vector.Y);

	public CameraConfiguration(Swapchain swapchain) {
		projection = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());
		controller = new OrbitalCameraController(cam, swapchain.extents());
		controller.radius(20);
		controller.scale(1f);
	}

	@Bean
	public ActionBindings bindings(Window window, RenderLoop loop, Animator animator) {
		// Bind stop action
		final ActionBindings bindings = new ActionBindings();
		final KeyboardDevice keyboard = window.keyboard();
		keyboard.bind(bindings);
		bindings.bind(keyboard.key("ESCAPE"), loop::stop);

		class PlayPauseAction implements ToggleHandler {
			@Override
			public void handle(boolean pressed) {
				if(animator.isPlaying()) {
					animator.apply(Player.State.PAUSE);
				}
				else {
					animator.apply(Player.State.PLAY);
				}
//				animator.apply(pressed ? Player.State.PAUSE : Player.State.PAUSE);
			}
		}
		bindings.bind(keyboard.key("SPACE"), new PlayPauseAction());

		// Bind camera controller
		final MouseDevice mouse = window.mouse();
		bindings.bind(mouse.pointer(), controller::update);
		bindings.bind(mouse.wheel(), controller::zoom);

		return bindings;
	}

	@Bean
	public static PushUpdateCommand update(PipelineLayout layout) {
		return PushUpdateCommand.of(layout);
	}

	@Bean
	Animator animator() {
		final Animator animator = new Animator(Duration.ofSeconds(10), new RotationAnimation(rot));
		animator.setRepeating(true);
		animator.apply(Player.State.PLAY);
		return animator;
	}

	@Bean
	public Task matrix(PushUpdateCommand update) {
		// Init projection matrix
		final BufferWrapper buffer = new BufferWrapper(update.data());
		buffer.insert(2, projection);

		// Update modelview matrix
		return () -> {
			final Matrix model = Rotation.matrix(rot);
			buffer.rewind();
			buffer.append(model);
			buffer.append(cam.matrix());
		};
	}
}
