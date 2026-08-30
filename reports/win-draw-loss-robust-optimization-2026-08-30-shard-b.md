# 共享界面口径赛事方案参数检查与重优化报告

- 生成时间：2026-08-30T11:34:55.559Z
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
- 本次优化范围：EUROPA_LEAGUE:CURRENT、CHAMPIONS_LEAGUE:PREVIOUS、CHAMPIONS_LEAGUE:CURRENT、PREMIER_LEAGUE:PREVIOUS、PREMIER_LEAGUE:CURRENT、LA_LIGA:PREVIOUS、LA_LIGA:CURRENT、BUNDESLIGA:PREVIOUS、BUNDESLIGA:CURRENT
- 验收逐赛事使用与界面相同的异步回测接口和共享推荐计算模块

| 赛事时段 | 方案 | 赔率样本 | 训练样本 | 验证样本 | 原采样率 | 新采样率 | 变化百分点 | 训练ROI | 验证ROI | 全量ROI | 命中率 | 推荐场次 | 投注数 | 净收益 | 波动率 | 处理方式 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 欧冠·含上届 | 稳健 | 248 | 173 | 75 | 66.94% | 0.00% | -66.94 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 英超·含上届 | 稳健 | 358 | 250 | 108 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 西甲·含上届 | 稳健 | 384 | 267 | 117 | 61.72% | 0.00% | -61.72 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 德甲·含上届 | 稳健 | 295 | 205 | 90 | 68.81% | 0.00% | -68.81 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 欧冠·含上届 | 激进 | 248 | 173 | 75 | 54.44% | 0.00% | -54.44 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 英超·含上届 | 激进 | 358 | 250 | 108 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 西甲·含上届 | 激进 | 384 | 267 | 117 | 59.64% | 0.00% | -59.64 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 德甲·含上届 | 激进 | 295 | 205 | 90 | 68.47% | 0.00% | -68.47 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 欧罗巴·仅本届 | 稳健 | 38 | 26 | 12 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 欧冠·仅本届 | 稳健 | 42 | 23 | 19 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 英超·仅本届 | 稳健 | 14 | 14 | 0 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 西甲·仅本届 | 稳健 | 25 | 17 | 8 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 德甲·仅本届 | 稳健 | 7 | 7 | 0 | 100.00% | 0.00% | -100.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 欧罗巴·仅本届 | 激进 | 38 | 26 | 12 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 欧冠·仅本届 | 激进 | 42 | 23 | 19 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 英超·仅本届 | 激进 | 14 | 14 | 0 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |
| 西甲·仅本届 | 激进 | 25 | 17 | 8 | 0.00% | 0.00% | 0.00 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | REOPTIMIZE_PAIR/CLOSED_CONSTRAINTS_NOT_MET |
| 德甲·仅本届 | 激进 | 7 | 7 | 0 | 28.57% | 0.00% | -28.57 | -- | -- | -- | -- | 0 | 0 | 0.00 | -- | FALLBACK_TO_PREVIOUS/CLOSED_INSUFFICIENT_VALIDATION_SAMPLE |

约束违规：0
