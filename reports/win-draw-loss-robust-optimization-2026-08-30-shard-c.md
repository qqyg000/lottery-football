# 共享界面口径赛事方案参数检查与重优化报告

- 生成时间：2026-08-30T10:07:02.279Z
- 配置状态：未应用
- 最终模拟次数：50,000
- 正式比赛权重下限：1.00
- 稳健方案ROI下限：5.00%
- 激进方案ROI必须严格高于同赛事同时段稳健方案
- 时间留出：按日期升序，前70.00%训练、后30.00%验证，同日比赛不跨分区
- 最少训练/验证样本：10/6，训练ROI下限0.00%，验证ROI下限0.00%
- 候选按训练集ROI与3个连续时间块稳定性排序，最终留出集只检查预先固定的候选池
- 无法通过稳健门禁的方案关闭，不产生投注推荐
- 默认稳健/激进采样率下限为60.00%/50.00%；欧洲杯、芬超、韩职仅本届使用现行专项下限，且激进不得高于稳健
- 激进方案采样率和ROI均高于稳健方案时，将激进参数移植给稳健方案，再优化激进方案
- 本次优化范围：SERIE_A:PREVIOUS、SERIE_A:CURRENT、LIGUE_1:PREVIOUS、LIGUE_1:CURRENT、PRIMEIRA_LIGA:PREVIOUS、PRIMEIRA_LIGA:CURRENT、EREDIVISIE:PREVIOUS、EREDIVISIE:CURRENT、ARGENTINE_PRIMERA_DIVISION:PREVIOUS
- 验收逐赛事使用与界面相同的异步回测接口和共享推荐计算模块

| 赛事时段 | 方案 | 赔率样本 | 训练样本 | 验证样本 | 原采样率 | 新采样率 | 变化百分点 | 训练ROI | 验证ROI | 全量ROI | 命中率 | 推荐场次 | 投注数 | 净收益 | 波动率 | 处理方式 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 意甲·含上届 | 稳健 | 372 | 260 | 112 | 65.59% | 0.00% | -65.59 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 法甲·含上届 | 稳健 | 233 | 163 | 70 | 61.37% | 0.00% | -61.37 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 葡超·含上届 | 稳健 | 203 | 142 | 61 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 荷甲·含上届 | 稳健 | 215 | 149 | 66 | 75.81% | 0.00% | -75.81 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 阿甲·含上届 | 稳健 | 0 | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | NO_DATA/NO_DATA |
| 意甲·含上届 | 激进 | 372 | 260 | 112 | 48.92% | 0.00% | -48.92 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 法甲·含上届 | 激进 | 233 | 163 | 70 | 50.64% | 0.00% | -50.64 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 葡超·含上届 | 激进 | 203 | 142 | 61 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 荷甲·含上届 | 激进 | 215 | 149 | 66 | 53.02% | 0.00% | -53.02 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 阿甲·含上届 | 激进 | 0 | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | NO_DATA/NO_DATA |
| 意甲·仅本届 | 稳健 | 15 | 15 | 0 | 100.00% | 0.00% | -100.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 法甲·仅本届 | 稳健 | 8 | 8 | 0 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 葡超·仅本届 | 稳健 | 24 | 16 | 8 | 66.67% | 0.00% | -66.67 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 荷甲·仅本届 | 稳健 | 24 | 11 | 13 | 83.33% | 70.83% | -12.50 | 62.00% | 3.50% | 32.75% | 52.94% | 17 | 20 | 6.55 | 1.61 | REOPTIMIZE_PAIR/OPTIMIZED |
| 意甲·仅本届 | 激进 | 15 | 15 | 0 | 33.33% | 0.00% | -33.33 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 法甲·仅本届 | 激进 | 8 | 8 | 0 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 葡超·仅本届 | 激进 | 24 | 16 | 8 | 54.17% | 0.00% | -54.17 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 荷甲·仅本届 | 激进 | 24 | 11 | 13 | 62.50% | 58.33% | -4.17 | 73.33% | 68.00% | 70.00% | 57.14% | 14 | 16 | 11.20 | 1.76 | REOPTIMIZE_PAIR/OPTIMIZED |

约束违规：0
