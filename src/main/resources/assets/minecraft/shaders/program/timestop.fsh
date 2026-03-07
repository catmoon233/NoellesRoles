#version 150

uniform sampler2D DiffuseSampler;
uniform float TimeProgress;   // 时间停止进度 (0~1)，控制动画强度
uniform float StopAmount;     // 灰白程度 (0~1)
uniform float TimeTotal;      // 累计时间，用于脉动动画

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    float d = length(uv - vec2(0.5, 0.5)); // 到屏幕中心的距离

    // 动态脉动因子 (0~1 之间变化)
    float pulse = (1.0 + sin(TimeTotal * 6.0)) * 0.5; // 频率可调
    float strength = TimeProgress * pulse;            // 动画强度
    strength = strength * strength;                   // 非线性增强

    // 色差偏移量：越靠近边缘偏移越大，最大偏移约 0.05 * TimeProgress
    float blur = strength * 0.05 * d;

    // 分别采样 R/G/B 通道，制造色差
    vec3 col;
    col.r = texture(DiffuseSampler, vec2(uv.x + blur, uv.y)).r;
    col.g = texture(DiffuseSampler, uv).g;
    col.b = texture(DiffuseSampler, vec2(uv.x - blur, uv.y)).b;

    // 计算灰度值 (用于灰白混合)
    float gray = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 grayColor = vec3(gray);

    // 根据 StopAmount 混合灰白
    col = mix(col, grayColor, StopAmount);

    // 根据 TimeProgress 添加轻微暗角，增强停止感
    col *= 1.0 - d * 0.5 * TimeProgress;

    fragColor = vec4(col, 1.0);
}