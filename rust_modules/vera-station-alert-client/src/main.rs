pub mod ui;

fn main() -> anyhow::Result<()> {
    env_logger::init();

    log::info!("Initializing user interface");
    let ui = ui::UserInterface::default();

    log::info!("User interface loop starting");
    ui.start_loop(|| Some(ui::UserInterfaceView::Clock(chrono::Local::now())));
    log::info!("User interface loop terminated");

    log::info!("Bye bye!");
    anyhow::Ok(())
}
