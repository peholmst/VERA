#include <SDL3_gfxPrimitives.h>

#include <algorithm>

#include "AnalogClock.hpp"
const uint32_t CLOCK_FACE_COLOR = RGBA(255, 255, 255);
const uint32_t CLOCK_BORDER_COLOR = RGBA(54, 69, 79);

static void PaintClockFace(SDL_Renderer *renderer, const float centerX, const float centerY, const float radius)
{
    // Clear
    SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
    SDL_RenderClear(renderer);

    // Border    
    filledCircleColor(renderer, centerX, centerY, radius, CLOCK_BORDER_COLOR);

    // Face
    filledCircleColor(renderer, centerX, centerY, radius * 0.95, CLOCK_FACE_COLOR);

    // TODO Ticks
}

static void PaintHands(SDL_Renderer *renderer, const float centerX, const float centerY, const float radius, const std::chrono::time_point<std::chrono::system_clock> &time)
{
    // TODO Paint the hands
}

AnalogClock::AnalogClock(const RendererPtr &renderer) : renderer(renderer)
{
    auto size = GetRendererSize(renderer);
    centerX = size.width / 2;
    centerY = size.height / 2;
    radius = std::min(size.height, size.width) / 2 - 20;
}

void AnalogClock::Paint()
{
    PaintClockFace(renderer.get(), centerX, centerY, radius);
    PaintHands(renderer.get(), centerX, centerY, radius, std::chrono::system_clock::now());
    SDL_RenderPresent(renderer.get());
}
