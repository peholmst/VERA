use sdl2::{event::Event, event::WindowEvent, render::Canvas, video::Window};

use super::UIDisplay;

pub struct SDLDisplay {
    sdl_context: sdl2::Sdl,
    canvas: Canvas<Window>,
    flags: SDLDisplayFlags,
}

struct SDLDisplayFlags {
    is_exit_requested: bool,
}

impl SDLDisplay {
    pub fn new() -> Self {
        let sdl_context = sdl2::init().expect("Failed to initialize SDL");
        let video = sdl_context.video().expect("Failed to initialize SDL");

        let window = video
            .window("VERA Station Alert Client", 1280, 720)
            .position_centered()
            .resizable()
            .build()
            .expect("Failed to create window");

        let canvas = window
            .into_canvas()
            .accelerated()
            .present_vsync()
            .build()
            .expect("Failed to create canvas");

        Self {
            sdl_context,
            canvas,
            flags: SDLDisplayFlags {
                is_exit_requested: false,
            },
        }
    }

    fn handle_window_event(&mut self, event: WindowEvent) -> anyhow::Result<()> {
        match event {
            WindowEvent::Close => {
                self.flags.is_exit_requested = true;
            }
            WindowEvent::Resized(..) => {}
            _ => {}
        }
        Ok(())
    }
}

impl Default for SDLDisplay {
    fn default() -> Self {
        SDLDisplay::new()
    }
}

impl UIDisplay for SDLDisplay {
    fn init(&mut self) -> anyhow::Result<()> {
        Ok(())
    }

    fn render_alert(
        &mut self,
        alert: &vera_station_alert_shared::message::AlertNotification,
    ) -> anyhow::Result<()> {
        todo!()
    }

    fn render_clock(&mut self, time: chrono::DateTime<chrono::Local>) -> anyhow::Result<()> {
        self.canvas.clear();
        Ok(())
    }

    fn cleanup(&mut self) -> anyhow::Result<()> {
        Ok(())
    }

    fn is_shutdown_requested(&mut self) -> bool {
        self.flags.is_exit_requested
    }

    fn handle_events(&mut self) -> anyhow::Result<()> {
        let mut event_pump = self
            .sdl_context
            .event_pump()
            .expect("Failed to access event pump");
        for event in event_pump.poll_iter() {
            if let Event::Window { win_event, .. } = event {
                self.handle_window_event(win_event)?;
            }
        }
        Ok(())
    }
}
