use std::{cell::Ref, cmp::min, f32::consts::PI};

use chrono::Timelike;
use raylib::{
    RaylibHandle, RaylibThread,
    color::Color,
    math::Vector2,
    prelude::{RaylibDraw, RaylibDrawHandle},
    text::{Font, RaylibFont},
};
use vera_station_alert_shared::message::{self, AlertNotification};

const BACKGROUND_COLOR: Color = Color::new(36, 37, 39, 100);
const CLOCK_FACE_COLOR: Color = Color::new(24, 25, 27, 100);
const CLOCK_NUMBERS_COLOR: Color = Color::WHITE;
const CLOCK_NUMBERS_FONT_SIZE: i32 = 90;
const CLOCK_HOUR_HAND_COLOR: Color = Color::WHITE;
const CLOCK_MINUTE_HAND_COLOR: Color = Color::WHITE;
const CLOCK_SECOND_HAND_COLOR: Color = Color::ORANGERED;

pub struct UserInterface {
    rl: RaylibHandle,
    thread: RaylibThread,
    font_cache: FontCache,
}

struct FontCache {
    clock_numbers_font: Font,
}

pub enum UserInterfaceView {
    Clock(chrono::DateTime<chrono::Local>),
    Alert(message::AlertNotification),
}

impl Default for UserInterface {
    fn default() -> Self {
        let (mut rl, thread) = raylib::init()
            .size(1280, 720)
            .title("VERA Station Alert Client")
            .resizable()
            .msaa_4x()
            .build();

        let clock_numbers_font_data = include_bytes!("../resources/Poppins-Bold.ttf");
        let clock_numbers_font = rl
            .load_font_from_memory(
                &thread,
                ".ttf",
                clock_numbers_font_data,
                CLOCK_NUMBERS_FONT_SIZE,
                Some("1234567890"),
            )
            .expect("Failed to load clock numbers font");

        let font_cache = FontCache { clock_numbers_font };

        Self {
            rl,
            thread,
            font_cache,
        }
    }
}

impl UserInterface {
    pub fn start_loop<F>(mut self, view_selector_fn: F)
    where
        F: Fn() -> Option<UserInterfaceView>,
    {
        let rl = &mut self.rl;
        rl.set_target_fps(10);
        while !rl.window_should_close() {
            rl.draw(&self.thread, |mut d| {
                d.clear_background(BACKGROUND_COLOR);
                if let Some(view) = view_selector_fn() {
                    match view {
                        UserInterfaceView::Clock(time) => {
                            draw_clock(&mut d, time, &self.font_cache);
                        }
                        UserInterfaceView::Alert(alert) => {
                            draw_alert(&mut d, alert);
                        }
                    }
                }
            });
        }
    }
}

// TODO Extract clock code into separate file

fn draw_clock(
    d: &mut RaylibDrawHandle<'_>,
    time: chrono::DateTime<chrono::Local>,
    font_cache: &FontCache,
) {
    let w = d.get_screen_width();
    let h = d.get_screen_height();
    let center = Vector2 {
        x: w as f32 / 2.0,
        y: h as f32 / 2.0,
    };
    let radius = center.x.min(center.y) - 20.0;
    d.draw_circle_v(center, radius, CLOCK_FACE_COLOR);

    // Draw numbers
    let number_font_size = radius * 0.20;
    let number_font = &font_cache.clock_numbers_font; // TODO Load correct size font
    let number_radius = radius * 0.8;
    draw_clock_number(
        d,
        center,
        number_radius,
        0.0,
        "12",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        30.0,
        "1",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        60.0,
        "2",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        90.0,
        "3",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        120.0,
        "4",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        150.0,
        "5",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        180.0,
        "6",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        210.0,
        "7",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        240.0,
        "8",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        270.0,
        "9",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        300.0,
        "10",
        number_font_size,
        number_font,
    );
    draw_clock_number(
        d,
        center,
        number_radius,
        330.0,
        "11",
        number_font_size,
        number_font,
    );

    // Draw hour hand
    let hour_degrees = ((time.hour() % 12) as f32 + time.minute() as f32 / 60.0) * 30.0;
    draw_clock_hand(
        d,
        center,
        radius * 0.5,
        hour_degrees,
        radius * 0.03,
        CLOCK_HOUR_HAND_COLOR,
    );

    // Draw minute hand
    let minute_degrees = time.minute() as f32 * 6.0;
    draw_clock_hand(
        d,
        center,
        radius * 0.65,
        minute_degrees,
        radius * 0.02,
        CLOCK_MINUTE_HAND_COLOR,
    );

    // Draw second hand
    let second_degrees = time.second() as f32 * 6.0;
    draw_clock_hand(
        d,
        center,
        radius * 0.7,
        second_degrees,
        radius * 0.01,
        CLOCK_SECOND_HAND_COLOR,
    );

    // Draw center
    d.draw_circle_v(center, radius * 0.03, CLOCK_SECOND_HAND_COLOR);
    d.draw_circle_v(center, radius * 0.01, CLOCK_MINUTE_HAND_COLOR);
}

fn draw_clock_number(
    d: &mut RaylibDrawHandle<'_>,
    center: Vector2,
    radius: f32,
    angle_degrees: f32,
    text: &str,
    font_size: f32,
    font: &Font,
) {
    let text_dimensions = font.measure_text(text, font_size, 1.0);

    let point = calculate_point_from_center(center, radius, angle_degrees.to_radians());

    let actual_x = point.x - text_dimensions.x / 2.0;
    let actual_y = point.y - text_dimensions.y / 2.0;

    d.draw_text_ex(
        font,
        text,
        Vector2::new(actual_x, actual_y),
        font_size,
        1.0,
        CLOCK_NUMBERS_COLOR,
    );
}

fn draw_clock_hand(
    d: &mut RaylibDrawHandle<'_>,
    center: Vector2,
    radius: f32,
    angle_degrees: f32,
    thickness: f32,
    color: Color,
) {
    let end_point = calculate_point_from_center(center, radius, angle_degrees.to_radians());
    // TODO Smooth the edges!
    d.draw_line_ex(center, end_point, thickness, color);
    d.draw_circle_v(end_point, thickness / 2.0, color);
}

fn calculate_point_from_center(center: Vector2, radius: f32, angle_radians: f32) -> Vector2 {
    let x_offset = radius * angle_radians.sin();
    let y_offset = radius * angle_radians.cos();

    Vector2 {
        x: center.x + x_offset,
        y: center.y - y_offset,
    }
}

fn draw_alert(d: &mut RaylibDrawHandle<'_>, alert: AlertNotification) {}
