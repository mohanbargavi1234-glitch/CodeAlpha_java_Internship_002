# Stock Trading & Portfolio Management System (Java)

A console-based Stock Trading Platform simulation built with Object-Oriented Programming (OOP) principles and CSV file I/O persistence.

## Features
- **Live Market Watch**: Real-time listing of stocks with current prices, daily price changes, and percentages.
- **Market Tick Simulation**: Simulates dynamic price fluctuations (-2.5% to +2.5%) across all tradeable tickers.
- **Buy & Sell Execution**: Validates cash balance, executes orders, updates portfolio holdings, and generates unique transaction receipts.
- **Portfolio Tracking**: Real-time computation of total invested capital, current market valuation, net worth, and unrealized profit/loss (P/L) breakdown per holding.
- **Transaction History**: Comprehensive ledger of all BUY and SELL transactions with timestamps and order IDs.
- **Fund Management**: Deposit and withdraw virtual cash.
- **Data Persistence**: Automatically stores and loads user account state (`user_data.txt`), active portfolio holdings (`portfolio.txt`), and transaction logs (`transactions.txt`).

## Architecture & OOP Design
| Class           | Responsibility |
|-----------------|----------------|
| `Stock`         | Represents a tradeable stock with ticker, company name, price, and price history. |
| `Holding`       | Represents shares owned of a single ticker and tracks weighted average cost basis. |
| `Portfolio`     | Manages user's stock holdings, calculates current market value and unrealized P/L. |
| `Transaction`   | Immutable record of a executed BUY/SELL order with timestamp and CSV parsing. |
| `User`          | Trader profile managing cash balance, net worth, and portfolio reference. |
| `Market`        | Holds exchange inventory and runs market price tick simulations. |
| `TradingEngine` | Validates and processes buy/sell orders, updates balances, and logs transactions. |
| `FileStorage`   | Handles persistence across runs (`user_data.txt`, `portfolio.txt`, `transactions.txt`). |
| `Main`          | Interactive console UI and menu system. |

## How to Compile & Run
```bash
javac *.java
java Main
```
