use std::{thread, time::Duration};

pub mod display;

fn main() -> anyhow::Result<()> {
    env_logger::init();

    log::info!("Initializing display");
    let mut display = display::create_display();
    display.init()?;

    log::info!("Main event loop starting");
    while !display.is_shutdown_requested() {
        display.handle_events()?;

        display.render_clock(chrono::Local::now())?;

        thread::sleep(Duration::from_millis(100));
    }
    log::info!("Main event loop terminated");

    display.cleanup()?;

    log::info!("Bye bye!");
    anyhow::Ok(())
}
