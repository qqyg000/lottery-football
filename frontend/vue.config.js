const path = require('path')

module.exports = {
  outputDir: path.resolve(__dirname, '../target/frontend-dist'),
  assetsDir: 'assets',
  productionSourceMap: false,
  publicPath: '/',
  devServer: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
}
