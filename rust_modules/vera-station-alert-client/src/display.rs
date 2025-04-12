use sdl::SDLDisplay;
use vera_station_alert_shared::message;

#[cfg(target_arch = "arm")]
pub mod framebuffer;
pub mod sdl;

/// Trait for the user interface display. The display can either render an alert or a wall clock.
/// The display will always be invoked from the main thread. If the implementation requires additional threads,
/// it should handle them internally.
pub trait UIDisplay {
    /// Initializes the display.
    fn init(&mut self) -> anyhow::Result<()>;

    /// Renders the given alert on the display.
    fn render_alert(&mut self, alert: &message::AlertNotification) -> anyhow::Result<()>;

    /// Renders a wall clock on the display.
    fn render_clock(&mut self, time: chrono::DateTime<chrono::Local>) -> anyhow::Result<()>;

    /// Cleans up any resources allocated by the display.
    fn cleanup(&mut self) -> anyhow::Result<()>;

    /// Handles any UI events. Called once every loop iteration.
    fn handle_events(&mut self) -> anyhow::Result<()>;

    /// Returns whether an application shutdown has been requested by the user.
    fn is_shutdown_requested(&mut self) -> bool;
}

/// Creates a new UIDisplay. This function should be called from the main thread. The returned object should always be invoked
/// from the main thread as well.
pub fn create_display() -> Box<dyn UIDisplay> {
    // TODO Add framebuffer support
    Box::new(SDLDisplay::new())
}
