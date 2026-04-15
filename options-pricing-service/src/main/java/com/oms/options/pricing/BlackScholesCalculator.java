package com.oms.options.pricing;

import com.oms.common.dto.OptionPriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Black-Scholes European Option Pricing Model
 *
 * Call: C = S*N(d1) - K*e^(-rT)*N(d2)
 * Put:  P = K*e^(-rT)*N(-d2) - S*N(-d1)
 *
 * d1 = [ln(S/K) + (r + σ²/2)*T] / (σ√T)
 * d2 = d1 - σ√T
 *
 * Greeks:
 *  Delta = N(d1)                          (call delta)
 *  Gamma = N'(d1) / (S * σ * √T)
 *  Vega  = S * N'(d1) * √T / 100
 *  Theta = [-(S*N'(d1)*σ)/(2√T) -rKe^(-rT)*N(d2)] / 365
 */
@Slf4j
@Component
public class BlackScholesCalculator {

    public OptionPriceDTO calculate(BlackScholesInput in) {
        double S = in.getUnderlyingPrice();
        double K = in.getStrikePrice();
        double r = in.getRiskFreeRate();
        double σ = in.getVolatility();
        double T = in.getTimeToExpiry();

        if (T <= 0 || σ <= 0 || S <= 0 || K <= 0) {
            log.warn("Invalid Black-Scholes inputs for {}: S={} K={} T={} σ={}", in.getSymbol(), S, K, T, σ);
            return OptionPriceDTO.builder().symbol(in.getSymbol()).calculatedAt(Instant.now()).build();
        }

        double sqrtT = Math.sqrt(T);
        double d1 = (Math.log(S / K) + (r + 0.5 * σ * σ) * T) / (σ * sqrtT);
        double d2 = d1 - σ * sqrtT;

        double callPrice = S * normCdf(d1) - K * Math.exp(-r * T) * normCdf(d2);
        double putPrice  = K * Math.exp(-r * T) * normCdf(-d2) - S * normCdf(-d1);

        // Greeks
        double nPrimeD1 = normPdf(d1);
        double delta    = normCdf(d1);
        double gamma    = nPrimeD1 / (S * σ * sqrtT);
        double vega     = S * nPrimeD1 * sqrtT / 100.0;
        double theta    = (-(S * nPrimeD1 * σ) / (2.0 * sqrtT)
                          - r * K * Math.exp(-r * T) * normCdf(d2)) / 365.0;

        log.debug("BSM {}: call={:.4f} put={:.4f} delta={:.4f}",
                in.getSymbol(), callPrice, putPrice, delta);

        return OptionPriceDTO.builder()
                .symbol(in.getSymbol())
                .underlyingPrice(S)
                .volatility(σ)
                .riskFreeRate(r)
                .timeToExpiry(T)
                .callPrice(roundTo4(callPrice))
                .putPrice(roundTo4(putPrice))
                .delta(roundTo4(delta))
                .gamma(roundTo6(gamma))
                .vega(roundTo4(vega))
                .theta(roundTo4(theta))
                .calculatedAt(Instant.now())
                .build();
    }

    /** Cumulative normal distribution — Abramowitz & Stegun approximation (error < 1.5e-7) */
    private double normCdf(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }

    /** Standard normal PDF */
    private double normPdf(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2.0 * Math.PI);
    }

    /** Error function — Abramowitz & Stegun 7.1.26 */
    private double erf(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429 * t
                             - 1.453152027) * t)
                             + 1.421413741) * t
                             - 0.284496736) * t
                             + 0.254829592) * t * Math.exp(-x * x);
        return x >= 0 ? y : -y;
    }

    private double roundTo4(double v) { return Math.round(v * 10000.0) / 10000.0; }
    private double roundTo6(double v) { return Math.round(v * 1000000.0) / 1000000.0; }
}
